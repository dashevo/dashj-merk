/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.merk

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.getUIntAtBE(idx: Int) =
    ((this[idx].toUInt() and 0xFFu) shl 24) or
        ((this[idx + 1].toUInt() and 0xFFu) shl 16) or
        ((this[idx + 2].toUInt() and 0xFFu) shl 8) or
        (this[idx + 3].toUInt() and 0xFFu)

fun ByteArray.getShortAtBE(idx: Int) =
    ((this[idx].toInt() and 0xFF) shl 24) or
        ((this[idx + 1].toInt() and 0xFF) shl 16)

fun ByteArray.getShortAtLE(idx: Int) =
    ((this[idx + 1].toInt() and 0xFF) shl 24) or
        ((this[idx].toInt() and 0xFF) shl 16)

fun ByteArray.getIntAtBE(idx: Int) =
    ((this[idx].toInt() and 0xFF) shl 24) or
        ((this[idx + 1].toInt() and 0xFF) shl 16) or
        ((this[idx + 2].toInt() and 0xFF) shl 8) or
        (this[idx + 3].toInt() and 0xFF)

fun ByteArray.getIntAtLE(idx: Int) =
    ((this[idx + 3].toInt() and 0xFF) shl 24) or
        ((this[idx + 2].toInt() and 0xFF) shl 16) or
        ((this[idx + 1].toInt() and 0xFF) shl 8) or
        (this[idx].toInt() and 0xFF)

fun ByteArray.getLongAtBE(idx: Int) =
    ((this[idx].toInt() and 0xFF) shl 56) or
        ((this[idx + 1].toInt() and 0xFF) shl 48) or
        ((this[idx + 2].toInt() and 0xFF) shl 40) or
        ((this[idx + 3].toInt() and 0xFF) shl 32) or
        ((this[idx + 4].toInt() and 0xFF) shl 24) or
        ((this[idx + 5].toInt() and 0xFF) shl 16) or
        ((this[idx + 6].toInt() and 0xFF) shl 8) or
        (this[idx + 7].toInt() and 0xFF)

fun ByteArray.getLongAtLE(idx: Int) =
    ((this[idx + 7].toInt() and 0xFF) shl 56) or
        ((this[idx + 6].toInt() and 0xFF) shl 48) or
        ((this[idx + 5].toInt() and 0xFF) shl 40) or
        ((this[idx + 4].toInt() and 0xFF) shl 32) or
        ((this[idx + 3].toInt() and 0xFF) shl 24) or
        ((this[idx + 2].toInt() and 0xFF) shl 16) or
        ((this[idx + 1].toInt() and 0xFF) shl 8) or
        (this[idx].toInt() and 0xFF)

fun ByteArray.getVarInt(offset: Int): Pair<Int, Long> {
    val first: Int = 0xFF and this[offset].toInt()
    return if (first < 253) {
        Pair(1, first.toLong())
    } else if (first == 253) {
        Pair(3, getShortAtLE(offset + 1).toLong())
    } else if (first == 254) {
        Pair(5, getIntAtLE(offset + 1).toLong())
    } else {
        Pair(9, getLongAtLE(offset + 1).toLong())
    }
}
