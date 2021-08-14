package mod.linguardium.open2lan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Copyright © 2020 by Lorenzo Magni
 * This file is part of MCLib.
 * MCLib is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class ZipUtil {
    public static void unzip(File src, File dst) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(src));
        ZipEntry entry = zis.getNextEntry();

        while (entry != null) {
            File dstFile = newFile(dst, entry);

            if (dstFile.isDirectory()) {
                entry = zis.getNextEntry();
                continue;
            }

            FileOutputStream os = new FileOutputStream(dstFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }

            os.close();
            entry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }
    private static File newFile(File dst, ZipEntry entry) throws IOException {
        File dstFile = new File(dst, entry.getName());

        String dirPath = dst.getCanonicalPath();
        String filePath = dstFile.getCanonicalPath();

        if (!filePath.startsWith(dirPath + File.separator))
            throw new IOException("Entry '" + entry.getName() + "' is outside of the target directory");

        if (entry.isDirectory()) {
            if (!dstFile.exists() || !dstFile.isDirectory()) {
                if (!dstFile.mkdirs()) throw new IOException("Cannot create '" + entry.getName() + "' directory");
            }
        }

        return dstFile;
    }
}
