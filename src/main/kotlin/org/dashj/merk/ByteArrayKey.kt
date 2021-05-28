package org.dashj.merk

class ByteArrayKey(private val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean =
        this === other || other is ByteArrayKey && this.bytes contentEquals other.bytes

    override fun hashCode(): Int = bytes.contentHashCode()
    override fun toString(): String = bytes.contentToString()
    fun toByteArray(): ByteArray {
        return bytes; }
}