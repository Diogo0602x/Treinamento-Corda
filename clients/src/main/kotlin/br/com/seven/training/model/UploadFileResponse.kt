package br.com.seven.training.model

import net.corda.core.crypto.SecureHash

data class UploadFileResponse(
        val fileHash: SecureHash
)