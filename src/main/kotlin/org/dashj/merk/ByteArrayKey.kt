package org.dashj.merk

import org.dashj.platform.dpp.toHexString

class ByteArrayKey(private val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean =
        this === other || other is ByteArrayKey && this.bytes contentEquals other.bytes

    override fun hashCode(): Int = bytes.contentHashCode()
    override fun toString(): String = bytes.toHexString()
    fun toByteArray(): ByteArray {
        return bytes; }
}