import com.seismic.utils.RandomHelper

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

RandomHelper.pick("A", "B", "C")

val hi = (int: Int) => int * 2
hi(4)

val a = ArrayBuffer(0, 2, 3)
a += 0
a

val x = ArrayBuffer("A", "B", "C", "D")
x.zipWithIndex.foldLeft("") { (acc, item) =>
  f"$acc, ${item._1} at ${item._2}"
}
x

val pattern = "([A-Z]{1}#{0,1})([0-9]{1})".r
pattern.findAllIn("C#3").matchData.toArray
val NOTE_NAMES = Array("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
0.to(127).map { i =>
  f"\"${NOTE_NAMES(i % 12)}${i / 12}\" -> $i,"
}.mkString("\n")


val foo = (int: Int) => (s: String) => 0 to int map { (i) => s + i }

foo(3)("hi")

4 to 7 map { (i) => i }

String.format("%4s", "23")
val s = "7890"
f"hi: $s%4s test"
"123456"
f"$s%6.6s hi"


Integer.toBinaryString(Integer.MAX_VALUE - 1)
Integer.toBinaryString(byte2Byte(0xf).toByte)
Integer.toBinaryString(1023)
Integer.toBinaryString(14 << 8)
Integer.toBinaryString(14) == "1110"
Integer.toBinaryString(11 << 10 | 1)
Integer.toBinaryString(14 << 10 | 1023)

Integer.parseInt("10011001", 2)
Integer.parseInt("1001100110011001", 2)

1023 >> 8 & 0xff
1023 & 0xff

Integer.toBinaryString(255)
0xff
0xf.asInstanceOf[Byte]
0xf.asInstanceOf[Byte].asInstanceOf[Int]
0xf.asInstanceOf[Int]

// XX11 1111 1111 = 1023
//      1111 1111 = 512

/**
  * Value: 0-1023, 10 bits LSB
  * Sensor: 0-16, 6 bits MSB
  * 0: kick, 1: snare, 2: handle
  *
  */
// 0000 0000 0000 0000
// XX11 1111 1111 1111   XXSS SSVV VVVV VVVV
// --SS SSVV VVVV VVVV
//   11 1011 1111 1111
2^3 + 2^2 + 2^1
8 + 4 + 2


