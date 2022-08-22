package util

import java.nio.ByteBuffer
import java.util.UUID


trait Identify[K] {

  def identify[M](v: M): K

}

object Identify {

  val uuidByteSize = 16

  implicit val intIdentify: Identify[Int] = new Identify[Int] {

    def identify[M](v: M): Int = v.hashCode

  }

  implicit val uuidIdentify: Identify[UUID] = new Identify[UUID] {

    def identify[M](v: M): UUID = {

      val bytes =
        ByteBuffer
          .allocate(uuidByteSize)
          .putInt(v.hashCode)
          .array()

      UUID.nameUUIDFromBytes(bytes)
    }

  }

}
