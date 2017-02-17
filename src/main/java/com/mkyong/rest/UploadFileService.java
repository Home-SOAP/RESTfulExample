package com.mkyong.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/file")
public class UploadFileService {

    /**
     * header sample
     * {
     * 		Content-Type=[image/png],
     * 		Content-Disposition=[form-data; name="file"; filename="filename.extension"]
     * }
     **/
    //get uploaded filename, is there a easy way in RESTEasy?
    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename:contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[]        name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

    //save to somewhere
    private void writeFile(byte[] content, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) file.createNewFile();

        FileOutputStream fop = new FileOutputStream(file);
        fop.write(content);
        fop.flush();
        fop.close();
    }

    /**
     * @param file
     * @return uploadFile is called, Uploaded file name : D:\server\apache-tomcat-7.0.70\photos\avatar.png
     */
	@POST
	@Path("/upload")
	@Consumes("multipart/form-data")
	public Response uploadFile(MultipartFormDataInput file) {
		String                      fName = "";
		Map<String, List<InputPart>> form = file.getFormDataMap();
		List<InputPart>             parts = form.get("file");

		for (InputPart part:parts) {
			try {
				//convert the uploaded file to inputstream
				InputStream inputStream = part.getBody(InputStream.class, null);
				byte[]            bytes = IOUtils.toByteArray(inputStream);
				
				//constructs upload file path
                MultivaluedMap<String, String> header = part.getHeaders();
                fName           = getFileName(header);
                /* Creating the directory to store file */
                String rootPath = System.getProperty("catalina.home");
                File        dir = new File(rootPath + File.separator + "photos");
                if (!dir.exists()) dir.mkdirs();
                /* Create the file on server */
                fName = dir.getAbsolutePath() + File.separator + fName;

				writeFile(bytes, fName);
				System.out.println("Done");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Response.status(200)
                .entity("Uploaded file name : " + fName)
                .build();
	}
}
