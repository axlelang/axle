
/**
 * Copyright (c) 2011-2013 Adam Pingel
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

import math.{ exp, log }
import spire.math._
import spire.implicits._
import spire.algebra._
import collection.mutable.Buffer
import org.joda.time.DateTime

package object axle {

  implicit val jodaDateTimeEq = new Eq[DateTime] {
    def eqv(x: DateTime, y: DateTime): Boolean = x.equals(y)
  }
  
  implicit def enrichGenSet[T](s: collection.GenSet[T]): EnrichedGenSet[T] = EnrichedGenSet(s)

  implicit def enrichGenSeq[T](genSeq: collection.GenSeq[T]): EnrichedGenSeq[T] = EnrichedGenSeq(genSeq)

  implicit def enrichGenTraversable[T: Manifest](gt: collection.GenTraversable[T]): EnrichedGenTraversable[T] = EnrichedGenTraversable(gt)

  implicit def enrichIndexedSeq[T: Manifest](is: IndexedSeq[T]): EnrichedIndexedSeq[T] = EnrichedIndexedSeq(is)

  implicit def enrichByteArray(barr: Array[Byte]): EnrichedByteArray = EnrichedByteArray(barr)

  implicit def enrichMutableBuffer[T](buffer: Buffer[T]): EnrichedMutableBuffer[T] = EnrichedMutableBuffer(buffer)

  implicit def enrichArray[T: Manifest](arr: Array[T]): EnrichedArray[T] = EnrichedArray(arr)

  implicit def enrichBoolean(b: Boolean): EnrichedBoolean = EnrichedBoolean(b)

  implicit def enrichInt(n: Int): EnrichedInt = EnrichedInt(n)

  def fib(n: Int): Int = (1 to n).foldLeft((1, 1))((pre, i) => (pre._2, pre._1 + pre._2))._1

  def recfib(n: Int): Int = n match { case 0 | 1 => 1 case _ => recfib(n - 2) + recfib(n - 1) }

  /**
   * http://en.wikipedia.org/wiki/Ackermann_function
   */

  def ackermann(m: Long, n: Long): Long =
    if (m === 0) {
      n + 1
    } else if (m > 0 && n === 0) {
      ackermann(m - 1, 1)
    } else {
      ackermann(m - 1, ackermann(m, n - 1))
    }

  // Fundamental:

  def id[A](x: A): A = x

  // IO

  def getLine(): String = scala.io.Source.stdin.getLines().next

  // List enrichments:

  def replicate[T](n: Int)(v: T): List[T] = (0 until n).map(i => v).toList

  def reverse[T](l: List[T]): List[T] = l.reverse

  def intersperse[T](d: T)(l: List[T]): List[T] =
    (0 until (2 * l.size - 1)).map(i => i % 2 match { case 0 => l(i / 2) case 1 => d }).toList

  // more math

  def square[N: Field](x: N): N = x ** 2

  def √[N: NRoot](x: N): N = x.sqrt

  implicit val stringEq = new Eq[String] {
    def eqv(x: String, y: String): Boolean = x equals y
  }

  implicit val boolEq = new Eq[Boolean] {
    def eqv(x: Boolean, y: Boolean): Boolean = x equals y
  }

  implicit def eqSet[S: Eq]: Eq[Set[S]] = new Eq[Set[S]] {
    def eqv(x: Set[S], y: Set[S]): Boolean = (x.size === y.size) && x.intersect(y).size === x.size
  }

}
