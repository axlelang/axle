
package axle

// http://en.wikipedia.org/wiki/Algebraic_structure

package object algebra {

  def ∅[Z](implicit z: Zero[Z]): Z = z.zero
  
  implicit def toIdent[A](a: A): Identity[A] = new Identity[A] {
    lazy val value = a
  }

  implicit def toMA[M[_], A](ma: M[A]): MA[M, A] = new MA[M, A] {
    val value = ma
  }

}
