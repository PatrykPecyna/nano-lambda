package nano;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.sql.*;

public class Main implements RequestHandler<RequestClass, ResponseClass> {

    public ResponseClass handleRequest(RequestClass request, Context context) {
        String timestamp = null;
        try {
            timestamp = go(request.getBlockId()).getTimestamp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseClass(timestamp);
    }

    private ResponseClass go(String request) throws Exception {
        String sshHost = "ssh.pythonanywhere.com";
        String sshUser = "nanoindexer";
        String sshPassword = "inzynieriaoprogramowania";
        String database = "nanoindexer$nano";
        String dbUser = "nanoindexer";
        String dbPassword = "inzynieria";
        int sshPort = 22;
        int tunnelLocalPort = 9080;
        String tunnelRemoteHost = "nanoindexer.mysql.pythonanywhere-services.com";
        int tunnelRemotePort = 3306;
        String driver = "com.mysql.cj.jdbc.Driver";
        String connectionUrl = "jdbc:mysql://localhost:" + tunnelLocalPort + "/";

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        JSch jsch= new JSch();
        Session session = jsch.getSession(sshUser,sshHost,sshPort);
        localUserInfo lui = new localUserInfo();
        session.setPassword(sshPassword);
        session.setConfig(config);
        session.setUserInfo(lui);
        session.connect();
        session.setPortForwardingL(tunnelLocalPort,tunnelRemoteHost,tunnelRemotePort);

        Class.forName(driver);
        Connection connection = DriverManager.getConnection(connectionUrl + database, dbUser, dbPassword);
        Statement statement = connection.createStatement();
        String query = "SELECT timestamp FROM nanosite_tabela WHERE block_id='" + request + "'";
        ResultSet resultSet = statement.executeQuery(query);

        ResponseClass responseTimestamp = null;
        while (resultSet.next()) {
            responseTimestamp = new ResponseClass(resultSet.getString(1));
        }
        connection.close();
        session.disconnect();

        return responseTimestamp;
    }

    class localUserInfo implements UserInfo {
        String passwd;
        public String getPassphrase() {
            return null;
        }
        public String getPassword() {
            return null;
        }
        public boolean promptPassword(String s) {
            return false;
        }
        public boolean promptPassphrase(String s) {
            return false;
        }
        public boolean promptYesNo(String s) {
            return false;
        }
        public void showMessage(String s) {}
    }
}
