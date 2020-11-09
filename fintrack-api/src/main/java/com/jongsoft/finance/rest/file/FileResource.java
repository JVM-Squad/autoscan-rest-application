package com.jongsoft.finance.rest.file;

import com.jongsoft.finance.StorageService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

@Tag(name = "Attachments")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/attachment")
public class FileResource {

    private final StorageService storageService;

    public FileResource(StorageService storageService) {
        this.storageService = storageService;
    }

    @Post
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload attachment",
            description = "Upload a file so that it can be attached to one of the entities in FinTrack"
    )
    UploadResponse upload(@Body CompletedFileUpload upload) throws IOException {
        var token = storageService.store(IOUtils.toByteArray(upload.getInputStream()));
        return new UploadResponse(token);
    }

    @Get("/{fileCode}")
    @Operation(
            summary = "Download attachment",
            description = "Download an existing attachment, if file encryption is enabled this will" +
                    " throw an exception if the current user did not upload the file."
    )
    byte[] download(@PathVariable String fileCode) {
        return storageService.read(fileCode);
    }

}
