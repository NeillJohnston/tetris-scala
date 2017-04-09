package io.github.neilljohnston

import scala.reflect.ClassTag

class Tetromino(val minoType: Int) {
    // Init matrix and coordinates
    var matrix: Array[Array[Int]] = Tetromino.types(minoType)
    var x: Int = 10 / 2 - matrix.length / 2
    var y: Int = 20
}

// Companion object for Tetromino
object Tetromino {
    // Init tetromino types
    // TODO change this to look prettier later
    final val types: Map[Int, Array[Array[Int]]] = Map(
        1 -> Array(Array(0,0,0,0), Array(0,0,0,0), Array(1,1,1,1), Array(0,0,0,0)),
        2 -> Array(Array(0,0,0), Array(1,1,1), Array(1,0,0)),
        3 -> Array(Array(0,0,0), Array(1,1,1), Array(0,0,1)),
        4 -> Array(Array(1,1), Array(1,1)),
        5 -> Array(Array(0,0,0), Array(1,1,0), Array(0,1,1)),
        6 -> Array(Array(0,0,0), Array(1,1,1), Array(0,1,0)),
        7 -> Array(Array(0,0,0), Array(0,1,1), Array(1,1,0)),
        0 -> Array(Array(1))
    )

    /**
      * Rotates a square matrix (NxN).
      * @param m    Matrix (a 2-dimensional array of type T) to rotate
      * @param r    Repeat rotation r times
      * @tparam T   Abstract type that the matrix holds
      * @return m rotated once clockwise
      */
    def rotate[T: ClassTag](m: Array[Array[T]], r: Int = 0): Array[Array[T]] = {
        val w = m.length - 1
        val m_ = Array.ofDim[T](w + 1, w + 1)

        // Perform the rotation
        for (x <- 0 to w; y <- 0 to w) m_(w - y)(x) = m(x)(y)
        if (r > 0) rotate[T](m_, r - 1) else m_
    }
}
