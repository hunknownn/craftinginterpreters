# Chapter 9 Challenges 1-2 정리

## 1. 조건문을 내장 문법 없이 구현하기

`if` 같은 조건문은 반드시 언어의 특별한 문법으로 만들 필요는 없다.
나중에 Lox가 다음 두 기능을 지원하면, 조건 분기를 객체와 함수만으로 구현할 수 있다.

- 일급 함수
- 동적 디스패치

### 일급 함수가 필요한 이유

`if`는 조건에 따라 둘 중 하나의 코드 블록만 실행해야 한다.

```lox
if (condition) {
  print "then";
} else {
  print "else";
}
```

이걸 메서드 호출로 바꾸려면 `then` 쪽 코드와 `else` 쪽 코드를 값처럼 넘길 수 있어야 한다.
즉, 실행할 코드를 함수로 감싸서 인자로 전달해야 한다.

```lox
condition.ifThenElse(thenBranch, elseBranch);
```

여기서 `thenBranch`와 `elseBranch`는 바로 실행되는 코드가 아니라, 나중에 필요한 쪽만 호출할 함수다.
함수를 변수에 담고, 인자로 넘기고, 나중에 호출할 수 있어야 하므로 일급 함수가 필요하다.

### 동적 디스패치가 필요한 이유

같은 메서드 호출이라도 receiver가 `true` 객체인지 `false` 객체인지에 따라 실행되는 구현이 달라져야 한다.

```lox
class True {
  ifThenElse(thenBranch, elseBranch) {
    return thenBranch();
  }
}

class False {
  ifThenElse(thenBranch, elseBranch) {
    return elseBranch();
  }
}
```

사용 예:

```lox
fun thenBranch() {
  print "then";
}

fun elseBranch() {
  print "else";
}

var trueValue = True();
var falseValue = False();

trueValue.ifThenElse(thenBranch, elseBranch);  // then
falseValue.ifThenElse(thenBranch, elseBranch); // else
```

`trueValue.ifThenElse(...)`와 `falseValue.ifThenElse(...)`는 같은 이름의 메서드를 호출하지만,
실제 객체가 `True`인지 `False`인지에 따라 다른 구현이 실행된다.
이것이 동적 디스패치다.

Java 개발자 관점에서는 `Runnable`이나 람다를 인자로 넘기고,
인터페이스 구현체의 오버라이딩 메서드가 실행되는 구조라고 보면 된다.

```java
interface LoxBool {
    void ifThenElse(Runnable thenBranch, Runnable elseBranch);
}

class LoxTrue implements LoxBool {
    public void ifThenElse(Runnable thenBranch, Runnable elseBranch) {
        thenBranch.run();
    }
}

class LoxFalse implements LoxBool {
    public void ifThenElse(Runnable thenBranch, Runnable elseBranch) {
        elseBranch.run();
    }
}
```

이 방식을 사용하는 대표적인 언어는 Smalltalk다.
Smalltalk에서는 조건 분기가 특별한 `if` 문법이라기보다 객체에게 메시지를 보내는 방식에 가깝다.

## 2. 반복문을 내장 문법 없이 구현하기

반복문도 `while`이나 `for`를 언어에 내장하지 않고 재귀 함수 호출로 표현할 수 있다.

예를 들어 다음 `for` 문은:

```lox
for (var i = 0; i < 100; i = i + 1) {
  print i;
}
```

재귀 함수로 이렇게 표현할 수 있다.

```lox
fun loop(i) {
  if (i < 100) {
    print i;
    loop(i + 1);
  }
}

loop(0);
```

하지만 일반적인 함수 호출은 호출할 때마다 call stack에 새 stack frame을 쌓는다.
반복 횟수가 많아지면 stack overflow가 날 수 있다.

그래서 반복을 재귀로 안정적으로 표현하려면 tail call optimization이 필요하다.

### Tail call optimization

tail call은 함수의 마지막 동작이 다른 함수를 호출하는 경우를 말한다.

```lox
fun f(x) {
  return g(x);
}
```

`g(x)`를 호출한 뒤 `f()`가 더 할 일이 없으므로, 현재 함수의 stack frame을 유지할 필요가 없다.
인터프리터나 컴파일러는 현재 frame을 버리거나 재사용하고 `g(x)`로 바로 점프할 수 있다.
이 최적화가 tail call optimization이다.

반대로 아래 코드는 tail call이 아니다.

```lox
fun f(x) {
  return g(x) + 1;
}
```

`g(x)`가 끝난 뒤 `+ 1`을 계산해야 하므로 `f()`의 stack frame을 유지해야 한다.

반복 재귀 예제의 `loop(i + 1)`은 함수의 마지막 동작이다.

```lox
fun loop(i) {
  if (i < 100) {
    print i;
    loop(i + 1);
  }
}
```

tail call optimization이 있으면 이 코드는 내부적으로 `while` 루프처럼 constant stack space로 실행될 수 있다.

Java식으로 비유하면:

```java
void loop(int i) {
    if (i < 100) {
        System.out.println(i);
        loop(i + 1);
    }
}
```

이 재귀를 런타임이나 컴파일러가 다음과 비슷하게 처리하는 것이다.

```java
void loop(int i) {
    while (i < 100) {
        System.out.println(i);
        i = i + 1;
    }
}
```

이 방식을 반복에 사용하는 대표적인 언어는 Scheme이다.
Scheme은 tail call optimization을 언어 차원에서 보장하기 때문에,
반복을 재귀로 표현하는 스타일이 자연스럽다.

