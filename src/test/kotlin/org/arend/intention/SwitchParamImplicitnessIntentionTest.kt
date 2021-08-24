package org.arend.intention

import org.arend.quickfix.QuickFixTestBase
import org.arend.util.ArendBundle

class SwitchParamImplicitnessIntentionTest : QuickFixTestBase() {
    private val fixName = ArendBundle.message("arend.coClause.switchParamImplicitness")
    private fun doTest(contents: String, result: String) = simpleQuickFixTest(fixName, contents, result)

    fun testFunctionImToExRemoveBraces() = doTest(
        """
        \func f (a b : Int) {p{-caret-} : a = b} => 0
        \func g => f 1 1 {idp}
        """,
        """
        \func f (a b : Int) (p{-caret-} : a = b) => 0
        \func g => f 1 1 idp
        """
    )

    fun testFunctionImToExLambdaInArg() = doTest(
        """
        \func f {A{-caret-} : \Type} {B : \Type} (a : A) (b : B) => (a, b)
        \func g => f 42 (\lam n => n + 1)
        """,
        """
        \func f (A{-caret-} : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => f _ 42 (\lam n => n + 1)
        """
    )

    fun testFunctionExToImAddBraces() = doTest(
        """
        \func f (a: b: : Int) (p{-caret-} : a: = b:) => 0
        \func g => f 1 1 idp
        """,
        """
        \func f (a: b: : Int) {p{-caret-} : a: = b:} => 0
        \func g => f 1 1 {idp}
        """
    )

    fun testFunctionImToExAddParam() = doTest(
        """
        \func id {A{-caret-} : \Type} => \lam (x : A) => x
        \func g => id 1
        """,
        """
        \func id (A{-caret-} : \Type) => \lam (x : A) => x
        \func g => id _ 1
        """
    )

    // Param after underscore is implicit
    fun testFunctionExToImUnderscore() = doTest(
        """
        \func id (A{-caret-} : \Type) => \lam (x : A) => x
        \func g => id _ 1
        """,
        """
        \func id {A{-caret-} : \Type} => \lam (x : A) => x
        \func g => id 1
        """
    )

    fun testFunctionExToImWithOmittedArgs() = doTest(
        """
        \func f {A B : \Type} ({-caret-}a : A) (b : B) => a
        \func test (a b : Nat) => f a b
        """,
        """
        \func f {A B : \Type} {{-caret-}a : A} (b : B) => a
        \func test (a b : Nat) => f {_} {_} {a} b
        """
    )

    fun testFunctionExToImLastParameter() = doTest(
        """
        \data D (a b : Nat) | con
        \func foo (a {-caret-}b : Nat) : D a b => con
        \func test : D 0 1 => foo _ _
        """,
        """
        \data D (a b : Nat) | con
        \func foo (a : Nat) {b{-caret-} : Nat} : D a b => con
        \func test : D 0 1 => foo _
        """
    )

    // Param after underscore is explicit
    fun testFunctionImToExUnderscore() = doTest(
        """
        \func kComb (A{-caret-} : \Type) {B : \Type} => \lam (a : A) (b : B) => a
        \func f => kComb _ {\Sigma Nat Nat} 1 (4, 2)
        """,
        """
        \func kComb {A{-caret-} : \Type} {B : \Type} => \lam (a : A) (b : B) => a
        \func f => kComb {_} {\Sigma Nat Nat} 1 (4, 2)
        """
    )

    fun testFunctionExToImParens() = doTest(
        """
        \func f (P{-caret-} : \Type) (p : P) => p
        \func g => f (\Sigma Nat Nat) (4, 2)
        """,
        """
        \func f {P{-caret-} : \Type} (p : P) => p
        \func g => f {(\Sigma Nat Nat)} (4, 2)
        """
    )

    fun testFieldImToExRemoveBraces() = doTest(
        """
        \class Test {X{-caret-} : \Type} (x : X)
        \func f => Test {\Sigma Nat Nat} (4, 2)
        """,
        """
        \class Test (X{-caret-} : \Type) (x : X)
        \func f => Test (\Sigma Nat Nat) (4, 2)
        """
    )

    fun testFieldExToImAddBraces() = doTest(
        """
        \class Test (X{-caret-} : \Type) (x : X)
        \func f => Test (\Sigma Nat Nat) (4, 2)
        """,
        """
        \class Test {X{-caret-} : \Type} (x : X)
        \func f => Test {(\Sigma Nat Nat)} (4, 2)
        """
    )

    fun testFieldImToExAddParam() = doTest(
        """
        \class Test {X{-caret-} : \Type} (x : X)
        \func f => Test 42
        """,
        """
        \class Test (X{-caret-} : \Type) (x : X)
        \func f => Test _ 42
        """
    )

    fun testFieldExToImUnderscore() = doTest(
        """
        \class Test (X{-caret-} : \Type) (x : X)
        \func f => Test _ 42
        """,
        """
        \class Test {X{-caret-} : \Type} (x : X)
        \func f => Test 42
        """
    )

    fun testFieldImToExUnderscore() = doTest(
        """
        \class Test {X{-caret-} : \Type} (x : X)
        \func f => Test 42
        """,
        """
        \class Test (X{-caret-} : \Type) (x : X)
        \func f => Test _ 42
        """
    )

    fun testTypeExToImAddBraces() = doTest(
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd (X{-caret-} : \Type) (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd Nat n => 2
        }
        """,
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd {{-caret-}X : \Type} (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd {Nat} n => 2
        }
        """
    )

    fun testTypeImToExRemoveBraces() = doTest(
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd {X{-caret-} : \Type} (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd {Nat} n => 2
        }
        """,
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd ({-caret-}X : \Type) (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd Nat n => 2
        }
        """
    )

    fun testTypeImToExAddParam() = doTest(
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd {X{-caret-} : \Type} (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd n => 2
        }
        """,
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd ({-caret-}X : \Type) (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd _ n => 2
        }
        """
    )

    fun testTypeExToImUnderscore() = doTest(
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd (X{-caret-} : \Type) (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd _ n => 2
        }
        """,
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd {{-caret-}X : \Type} (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd n => 2
        }
        """
    )

    fun testTypeImToExUnderscore() = doTest(
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd (X{-caret-} : \Type) (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd _ n => 2
        }
        """,
        """
        \record testRecord (T : \Type)
          | fst (X : T) : Nat
          | snd {{-caret-}X : \Type} (n : X) : Nat

        \func h => \new testRecord (\Sigma Nat Nat) {
          | fst X => 1
          | snd n => 2
        }
        """
    )

//    TODO
//    fun testTypeImToExUnderscoreEnd() = doTest(
//        """
//        \record testRecord (T : \Type)
//          | k {A B : \Type} (a : A) (b :{-caret-} B) : A
//
//        \func h => \new testRecord (\Sigma Nat Nat) {
//          | k a _ => a
//        }
//        """,
//        """
//        \record testRecord (T : \Type)
//          | k {A B : \Type} (a : A) {b {-caret-}: B} : A
//
//        \func h => \new testRecord (\Sigma Nat Nat) {
//          | k a {_} => a
//        }
//        """
//    )

    fun testFunctionImToExSplitTeleA() = doTest(
        """
        \func k {{-caret-}A B : \Type} (a : A) (b : B) => a
        \func f => k {_} 1 (1, 2)
        """,
        """
        \func k ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => a
        \func f => k _ 1 (1, 2)
        """
    )

    fun testFunctionImToExSplitTeleB() = doTest(
        """
        \func k {A {-caret-}B : \Type} (a : A) (b : B) => a
        \func f => k {_} 1 (1, 2)
        """,
        """
        \func k {A : \Type} (B{-caret-} : \Type) (a : A) (b : B) => a
        \func f => k {_} _ 1 (1, 2)
        """
    )

    fun testFieldImToExSplitTeleA() = doTest(
        """
        \class Test {{-caret-}A B : \Type} (a : A) (b : B)
        \func f => Test {Nat} 1 (4, 2)
        """,
        """
        \class Test ({-caret-}A : \Type) {B : \Type} (a : A) (b : B)
        \func f => Test Nat 1 (4, 2)
        """
    )

    fun testFieldImToExSplitTeleB() = doTest(
        """
        \class Test {A {-caret-}B : \Type} (a : A) (b : B)
        \func f => Test {Nat} 1 (4, 2)
        """,
        """
        \class Test {A  : \Type} (B : \Type) (a : A) (b : B)
        \func f => Test {Nat} _ 1 (4, 2)
        """
    )

    fun testTypeImToExSplitA() = doTest(
        """
        \record testRecord (T : \Type)
          | k {{-caret-}A B : \Type} (a : A) (b : B) : A

        \func h => \new testRecord (\Sigma Nat Nat) {
          | k {_} a b => a
        }
        """,
        """
        \record testRecord (T : \Type)
          | k ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) : A

        \func h => \new testRecord (\Sigma Nat Nat) {
          | k _ a b => a
        }
        """
    )

    fun testTypeImToExSplitB() = doTest(
        """
        \record testRecord (T : \Type)
          | k {A {-caret-}B : \Type} (a : A) (b : B) : A

        \func h => \new testRecord (\Sigma Nat Nat) {
          | k {_} a b => a
        }
        """,
        """
        \record testRecord (T : \Type)
          | k {A : \Type} ({-caret-}B : \Type) (a : A) (b : B) : A

        \func h => \new testRecord (\Sigma Nat Nat) {
          | k {_} _ a b => a
        }
        """
    )

    fun testTypeExToImParamAfterEx() = doTest(
        """
        \record testRecord (T : \Type)
          | k {A B : \Type} ({-caret-}a : A) (b : B) : A

        \func h => \new testRecord (\Sigma Nat Nat) {
          | k {_} a b => a
        }
        """,
        """
        \record testRecord (T : \Type)
          | k {A B : \Type} {{-caret-}a : A} (b : B) : A

        \func h => \new testRecord (\Sigma Nat Nat) {
          | k {_} {_} {a} b => a
        }
        """
    )

    fun testTypeImToExAllTele() = doTest(
        """
        \record testRecord (T : \Type)
          | k {A B : {-caret-}\Type} (a : A) (b : B) : A

        \func h => \new testRecord (\Sigma Nat Nat) {
          | k a b => a
        }
        """,
        """
        \record testRecord (T : \Type)
          | k (A B : \Type) {-caret-}(a : A) (b : B) : A

        \func h => \new testRecord (\Sigma Nat Nat) {
          | k _ _ a b => a
        }
        """
    )

    fun testFunctionImToExInfixSimple() = doTest(
        """
        \func mp {{-caret-}A B : \Type} (a : A) (b : B) => a
        \func g => 42 `mp` 41
        """,
        """
        \func mp ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => a
        \func g => mp _ 42 41
        """
    )


    fun testFunctionImToExInfixChain() = doTest(
        """
        \func \infixl 6 !+! {{-caret-}A B : \Type} (a : A) (b : B) => (a, b)
        \func f => ((1, 2) !+! (3, 4)) !+! (5, 6)
        """,
        """
        \func \infixl 6 !+! ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func f => !+! _ (!+! _ (1, 2) (3, 4)) (5, 6)
        """
    )

    fun testFunctionImToExOtherInfixOnTop() = doTest(
        """
        \open Nat (+)
        \func f (a {-caret-}b : Nat) => a
        \func test => f 0 1 + 2
        """,
        """
        \open Nat (+)
        \func f (a : Nat) {b{-caret-} : Nat} => a
        \func test => f 0 {1} + 2
        """
    )

    fun testFunctionImToExSaveModule() = doTest(
        """
        \func f {A B : \Type} ({-caret-}a : A) (b : B) => a
        \func test (a b : Nat) => (f 1 2 Nat.+ 42) Nat.* 43
        """,
        """
        \func f {A B : \Type} {{-caret-}a : A} (b : B) => a
        \func test (a b : Nat) => (f {_} {_} {1} 2 Nat.+ 42) Nat.* 43
        """
    )

    fun testFunctionExToImInfixAddBraces() = doTest(
        """
        \func mp ({-caret-}A : \Type) {B : \Type} (a : A) (ab : A -> B) => ab a
        \func g => (Nat `mp` 42) (\lam n => n + 1)
        """,
        """
        \func mp {{-caret-}A : \Type} {B : \Type} (a : A) (ab : A -> B) => ab a
        \func g => (mp {Nat} 42) (\lam n => n + 1)
        """
    )

    fun testFunctionImToExInfixAddUnderscore() = doTest(
        """
        \func mp (A : \Type) {{-caret-}B : \Type} (a : A) (ab : A -> B) => ab a
        \func g => (Nat `mp` 42) (\lam n => n + 1)
        """,
        """
        \func mp (A : \Type) ({-caret-}B : \Type) (a : A) (ab : A -> B) => ab a
        \func g => (mp Nat _ 42) (\lam n => n + 1)
        """
    )

    fun testFunctionImToExInfixRemoveBraces() = doTest(
        """
        \func mp {{-caret-}A B : \Type} (a : A) (ab : A -> B) => ab a
        \func g => 42 `mp` {Nat} {\Sigma Nat Nat} (\lam n => (n, n + 1))
        """,
        """
        \func mp ({-caret-}A : \Type) {B : \Type} (a : A) (ab : A -> B) => ab a
        \func g => mp Nat {\Sigma Nat Nat} 42 (\lam n => (n, n + 1))
        """
    )

    fun testFunctionImToExAddUnderscore1() = doTest(
        """
        \func f {A B : \Type} (a : A) {{-caret-}C : \Type} (b : B) (c : C) => (a, (b, c))
        \func g => (1 `f` {_} {_} 2) 3
        """,
        """
        \func f {A B : \Type} (a : A) ({-caret-}C : \Type) (b : B) (c : C) => (a, (b, c))
        \func g => (f {_} {_} 1 _ 2) 3
        """
    )

    fun testOperatorImToExAddBraces() = doTest(
        """
        \func \infix 6 !+! {{-caret-}A B : \Type} (a : A) (b : B) => (a, b)
        \func g => 1 !+! 2
        """,
        """
        \func \infix 6 !+! ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => !+! _ 1 2
        """
    )

    fun testOperatorImToExWithImplicitA() = doTest(
        """
        \func \infix 6 !+! {{-caret-}A B : \Type} (a : A) (b : B) => (a, b)
        \func g => 1 !+! {Nat} 2
        """,
        """
        \func \infix 6 !+! ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => !+! Nat 1 2
        """
    )

    fun testOperatorImToExWithImplicitB() = doTest(
        """
        \func \infix 6 !+! {A {-caret-}B : \Type} (a : A) (b : B) => (a, b)
        \func g => 1 !+! {Nat} 2
        """,
        """
        \func \infix 6 !+! {A : \Type} (B{-caret-} : \Type) (a : A) (b : B) => (a, b)
        \func g => !+! {Nat} _ 1 2
        """
    )

    fun testOperatorExToImAddUnderscore() = doTest(
        """
        \func \infix 6 !+! ({-caret-}A B : \Type) (a : A) (b : B) => (a, b)
        \func g => (Nat !+! Nat) 1 2
        """,
        """
        \func \infix 6 !+! {{-caret-}A : \Type} (B : \Type) (a : A) (b : B) => (a, b)
        \func g => (!+! {Nat} Nat) 1 2
        """
    )

    fun testFunctionImToExComposition1() = doTest(
        """
        \func id {{-caret-}A : \Type} (a : A) => a
        \func p {A : \Type} (a : A) => (a, a)
        \func g => p (id 42)
        """,
        """
        \func id ({-caret-}A : \Type) (a : A) => a
        \func p {A : \Type} (a : A) => (a, a)
        \func g => p (id _ 42)
        """
    )

    fun testFunctionImToExComposition2() = doTest(
        """
        \func id {A : \Type} (a : A) => a
        \func p {{-caret-}A : \Type} (a : A) => (a, a)
        \func g => p (id 42)
        """,
        """
        \func id {A : \Type} (a : A) => a
        \func p ({-caret-}A : \Type) (a : A) => (a, a)
        \func g => p _ (id 42)
        """
    )

    fun testFunctionImToExPair() = doTest(
        """
        \func id {A{-caret-} : \Type} (a : A) => a
        \func g => (id 42, id {Nat} 43)
        """,
        """
        \func id (A{-caret-} : \Type) (a : A) => a
        \func g => (id _ 42, id Nat 43)
        """
    )

    fun testFunctionImToExCallChain() = doTest(
        """
        \func p {A{-caret-} : \Type} (a : A) => (a, a)
        \func g => (p (p 42), p 43)
        """,
        """
        \func p (A{-caret-} : \Type) (a : A) => (a, a)
        \func g => (p _ (p _ 42), p _ 43)
        """
    )

    fun testOperatorImToExPair() = doTest(
        """
        \func \infix 6 !+! {{-caret-}A B : \Type} (a : A) (b : B) => (a, b)
        \func g => (1 !+! 2, 3 !+! {Nat} 4)
        """,
        """
        \func \infix 6 !+! ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => (!+! _ 1 2, !+! Nat 3 4)
        """
    )

    fun testOperatorImToExMixStyle() = doTest(
        """
        \func p {A{-caret-} : \Type} {B : \Type} (a : A) (b : B) => (a, b)
        \func g => p 1 (p 2 (3 `p` {Nat} (p {_} 4 5)))
        """,
        """
        \func p (A{-caret-} : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => p _ 1 (p _ 2 (p Nat 3 (p _ 4 5)))
        """
    )

    fun testOperatorImToExInfixrChain() = doTest(
        """
        \func \infixr 6 !+! {{-caret-}A B : \Type} (a : A) (b : B) => (a, b)
        \func g => 1 !+! 2 !+! 3
        """,
        """
        \func \infixr 6 !+! ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => !+! _ 1 (!+! _ 2 3)
        """
    )

    fun testOperatorImToExInfixlChain() = doTest(
        """
        \func \infixl 6 !+! {{-caret-}A B : \Type} (a : A) (b : B) => (a, b)
        \func g => 1 !+! 2 !+! 3
        """,
        """
        \func \infixl 6 !+! ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => !+! _ (!+! _ 1 2) 3
        """
    )

    fun testOperatorSameArguments() = doTest(
        """
        \func k {{-caret-}A B : \Type} (a : A) (b : B) => (a, b)
        \func g => k (k 2 3) (k (k 2 3) 3)
        """,
        """
        \func k ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => k _ (k _ 2 3) (k _ (k _ 2 3) 3)
        """
    )

    fun testFunctionImToExPartialApp1() = doTest(
        """
        \func f {A {-caret-}B : \Type} (a : A) (b : B) => (a, b)
        \func g => f {Nat}
        """,
        """
        \func f {A : \Type} (B{-caret-} : \Type) (a : A) (b : B) => (a, b)
        \func g => (\lam {B} a b => f {Nat} B a b)
        """
    )

    fun testFunctionImToExPartialApp2() = doTest(
        """
        \func f {{-caret-}A B : \Type} (a : A) (b : B) => (a, b)
        \func g => f {Nat}
        """,
        """
        \func f ({-caret-}A : \Type) {B : \Type} (a : A) (b : B) => (a, b)
        \func g => f Nat
        """
    )

    fun testFunctionImToExPartialApp3() = doTest(
        """
        \func f {A {-caret-}B : \Type} (a : A) (b : B) => (a, b)
        \func id {A : \Type} (a : A) => a
        \func g => id (f {Nat})
        """,
        """
        \func f {A : \Type} (B{-caret-} : \Type) (a : A) (b : B) => (a, b)
        \func id {A : \Type} (a : A) => a
        \func g => id (\lam {B} a b => f {Nat} B a b)
        """
    )

    fun testFunctionExToImPartialApp1() = doTest(
        """
        \func f (A {-caret-}B : \Type) (a : A) (b : B) => (a, b)
        \func g => f Nat
        """,
        """
        \func f (A : \Type) {B{-caret-} : \Type} (a : A) (b : B) => (a, b)
        \func g => (\lam B a b => f Nat {B} a b)
        """
    )

    fun testFunctionExToImPartialApp2() = doTest(
        """
        \func p (a {-caret-}b : Nat) => a
        \func test (f : (Nat -> Nat -> Nat) -> Nat) => f (p)
        """,
        """
        \func p (a : Nat) {b{-caret-} : Nat} => a
        \func test (f : (Nat -> Nat -> Nat) -> Nat) => f (\lam a b => p a {b})
        """
    )

    fun testFunctionExToImPartialApp3() = doTest(
        """
        \func p (a {-caret-}b : Nat) => a
        \func test (f : (Nat -> Nat -> Nat) -> Nat) => f p
        """,
        """
        \func p (a : Nat) {b{-caret-} : Nat} => a
        \func test (f : (Nat -> Nat -> Nat) -> Nat) => f (\lam a b => p a {b})
        """
    )

    fun testOperatorImToExPartialApp1() = doTest(
        """
        \func \infixl 6 !+! {A B : \Type} (a : A) ({-caret-}b : B) => (a, b)
        \func g => 1 !+! {Nat} {Nat}
        """,
        """
        \func \infixl 6 !+! {A B : \Type} (a : A) {{-caret-}b : B} => (a, b)
        \func g => (\lam b => !+! {Nat} {Nat} 1 {b})
        """
    )

    fun testFieldImToExPartialApp1() = doTest(
        """
        \class Test {A {-caret-}B : \Type} (a : A) (b : B)
        \func f => Test {Nat}
        """,
        """
        \class Test {A  : \Type} (B{-caret-} : \Type) (a : A) (b : B)
        \func f => (\lam {B} a b => Test {Nat} B a b)
        """
    )

    fun testFunctionImToExPartialAppCorrectNaming() = doTest(
        """
        \func foo (a {-caret-}b b1 : Nat) => a
        \func test => \lam (a : Nat) (b : Nat) (b1 : Nat) => foo 0
        """,
        """
        \func foo (a : Nat) {b{-caret-} : Nat} (b1 : Nat) => a
        \func test => \lam (a : Nat) (b : Nat) (b1 : Nat) => (\lam b2 b3 => foo 0 {b2} b3)
        """
    )
}
