package br.com.seven.training.controller

import br.com.seven.training.corda.Attachment
import br.com.seven.training.corda.NodeRPCConnection
import br.com.seven.training.model.UploadFileResponse
import net.corda.core.crypto.SecureHash
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("file")
class FileController {
    @Autowired
    private lateinit var nodeRPCConnection: NodeRPCConnection

    @PostMapping(
            consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
            produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    fun upload(
            @RequestParam("file", required = true) file: MultipartFile
    ): UploadFileResponse {
        return UploadFileResponse(nodeRPCConnection.saveAttachment(Attachment(file)))
    }

    @GetMapping(
            value = ["/{hash}"],
            produces = [MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE]
    )
    fun download(
            @PathVariable(value = "hash", name = "hash", required = true) hash: String
    ): ByteArrayResource {
        return Attachment.unwrapAttachment(nodeRPCConnection.openAttachment(SecureHash.parse(hash)))
    }
}