package com.dyman.show3dmodel.bean;

/**
 * Created by dyman on 16/8/18.
 */
public class FileBean {
    public static final String ID = "fileID";
    public static final String FILEPATH = "filePath";
    public static final String FILENAME = "fileName";
    public static final String FILETYPE = "fileType";
    public static final String CREATETIME = "createTime";

    private String id;
    private String filePath;
    private String fileName;
    private String fileType;
    private String createTime;

    public FileBean() {
        super();
    }

    public String getId() { return id;}

    public void setId(String id) { this.id = id;}

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "createTime='" + createTime + '\'' +
                ", id='" + id + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}
