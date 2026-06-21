package org.lab.telegram_bot.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class MultipartFileUtil {

    private MultipartFileUtil() {}


    public static MultipartFile create(byte[] bytes, org.telegram.telegrambots.meta.api.objects.File file, InputStream in) {
        String[] strings = file.getFilePath().split("/");
        String filename = strings[strings.length - 1];
        return MyMultipartFile.builder()
                .name(filename)
                .originalFilename(filename)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .bytes(bytes)
                .inputStream(in)
                .build();
    }


    @Setter
    @Builder
    @AllArgsConstructor
    public static class MyMultipartFile implements MultipartFile {

        private String name;
        private String originalFilename;
        private String contentType;
        private long size;
        private byte[] bytes;
        private InputStream inputStream;


        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return size <= 0;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return bytes;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream out = new FileOutputStream(dest)) {
                out.write(bytes);
            }
        }
    }
}
