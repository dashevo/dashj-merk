/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.merk

import org.dashj.platform.dpp.toHex

class ByteArrayKey(private val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean =
        this === other || other is ByteArrayKey && this.bytes contentEquals other.bytes

    override fun hashCode(): Int = bytes.contentHashCode()
    override fun toString(): String = bytes.toHex()
    fun toByteArray(): ByteArray {
        return bytes
    }
}
