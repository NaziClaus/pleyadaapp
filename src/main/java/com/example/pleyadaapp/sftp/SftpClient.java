package com.example.pleyadaapp.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Vector;

@Component
public class SftpClient {

    @Value("${SFTP_HOST}")
    private String host;
    @Value("${SFTP_PORT:22}")
    private int port;
    @Value("${SFTP_USER}")
    private String user;
    @Value("${SFTP_PASS}")
    private String pass;

    public interface ChannelCallback<T> {
        T doWithChannel(ChannelSftp channel) throws SftpException;
    }

    public <T> T execute(ChannelCallback<T> callback) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(pass);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            try {
                return callback.doWithChannel(channel);
            } finally {
                channel.disconnect();
            }
        } finally {
            session.disconnect();
        }
    }

    public Vector<ChannelSftp.LsEntry> list(String path) throws JSchException, SftpException {
        return execute(channel -> channel.ls(path));
    }

    public InputStream download(String remote) throws JSchException, SftpException {
        return execute(channel -> channel.get(remote));
    }
}
