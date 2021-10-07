/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.merk

import io.github.rctcwyvrn.blake3.Blake3
import java.security.MessageDigest

fun blake3(data: ByteArray): ByteArray {
    val hasher = Blake3.newInstance()
    hasher.update(data)
    return hasher.digest()
}

private var digest = MessageDigest.getInstance("SHA-256")

fun sha256(data: ByteArray): ByteArray {
    return digest.digest(data)
}
