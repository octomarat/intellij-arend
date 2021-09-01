package org.arend.intention

import org.arend.quickfix.QuickFixTestBase
import org.arend.util.ArendBundle
import org.intellij.lang.annotations.Language

class ExtractExpressionToFunctionIntentionTest : QuickFixTestBase() {
    private fun doTest(@Language("Arend") contents: String, @Language("Arend") result: String) =
        simpleQuickFixTest(ArendBundle.message("arend.generate.function.from.expression"), contents.trimIndent(), result.trimIndent())

    fun `test extract from selection`() = doTest("""
        \func bar (n : Nat) : Nat => n Nat.+ 1

        \func foo : Nat => bar {-selection-}({-caret-}10 Nat.+ 10){-end_selection-}
    """, """
        \func bar (n : Nat) : Nat => n Nat.+ 1

        \func foo : Nat => bar (foo-lemma)
    
        \func foo-lemma : Fin 21 => 20
    """)

    fun `test extract from selection 2`() = doTest("""
        \func bar {A : \Prop} (x y : A) : x = y => {-selection-}Path.i{-caret-}nProp{-end_selection-} _ _
    """, """
        \func bar {A : \Prop} (x y : A) : x = y => bar-lemma x y

        \func bar-lemma {A : \Prop} (x y : A) : x = y => Path.inProp x y
    """)

    fun `test extract from selection 3`() = doTest("""
        \func bar {A : \Prop} (x y : A) : x = y => Path.inProp {-selection-}{-caret-}_{-end_selection-} _
    """, """
        \func bar {A : \Prop} (x y : A) : x = y => Path.inProp (bar-lemma x) _

        \func bar-lemma {A : \Prop} (x : A) : A => x
    """)

    fun `test implicit args`() = doTest("""
        \data D {x y : Nat} {eq : x = y} (z : Nat) | dd

        \func foo : D {2} {2} {idp} 1 => {-selection-}{-caret-}dd{-end_selection-}
    """, """
        \data D {x y : Nat} {eq : x = y} (z : Nat) | dd

        \func foo : D {2} {2} {idp} 1 => foo-lemma
  
        \func foo-lemma : D {2} {2} {idp {Nat} {2}} 1 => dd
    """)

    fun `test qualified definition`() = doTest("""
        \func foo : Nat => {-selection-}b{-caret-}ar{-end_selection-}
        \where {
            \func bar : Nat => 1
        }
    """, """
        \func foo : Nat => foo-lemma
        \where {
            \func bar : Nat => 1
        }
        
        \func foo-lemma : Nat => foo.bar
    """.trimIndent())

    fun `test projection`() = doTest("""
        \func foo : \Sigma Nat Nat => (1, 1)

        \func bar => \let (a, b) => foo \in {-selection-}{-caret-}a{-end_selection-}
    """, """
        \func foo : \Sigma Nat Nat => (1, 1)

        \func bar => \let (a, b) => foo \in bar-lemma a
        
        \func bar-lemma (a : Nat) : Nat => a 
    """)

    fun `test nested projection`() = doTest("""
        \func foo : \Sigma (\Sigma Nat Nat) Nat => ((1, 1), 1)

        \func bar : \Sigma Nat Nat => \let ((a, b), c) => foo \in {-selection-}(a{-caret-}, c){-end_selection-}
    """, """
        \func foo : \Sigma (\Sigma Nat Nat) Nat => ((1, 1), 1)

        \func bar : \Sigma Nat Nat => \let ((a, b), c) => foo \in bar-lemma a c
        
        \func bar-lemma (a c : Nat) : \Sigma Nat Nat => (a, c) 
    """)

    fun `test projections 2`() = doTest("""
        \func f : \Sigma Nat Nat => (1, 2)
        
        \func g : Nat -> Nat => \lam x => x
        
        \func foo : Nat => \let rr => f \in {-selection-}g {-caret-}rr.1{-end_selection-}
    """, """
        \func f : \Sigma Nat Nat => (1, 2)
        
        \func g : Nat -> Nat => \lam x => x
        
        \func foo : Nat => \let rr => f \in foo-lemma rr
        
        \func foo-lemma (rr : \Sigma Nat Nat) : Nat => g rr.1""")

    fun `test complex infix`() = doTest("""
        \func foo (x y : Nat) => {-selection-}x Nat{-caret-}.+ x{-end_selection-} Nat.+ y
    """, """
        \func foo (x y : Nat) => (foo-lemma x) Nat.+ y
        
        \func foo-lemma (x : Nat) : Nat => x Nat.+ x
    """)
}
