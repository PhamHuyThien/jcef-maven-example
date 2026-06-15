package home.thienph.controllers;

import home.thienph.Main;
import home.thienph.anotations.CefController;
import home.thienph.anotations.OnCefMessage;
import home.thienph.data.dto.SayHelloReq;
import home.thienph.jcefs.JcefFrame;
import home.thienph.managers.JcefManager;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

@CefController
public class AppController {

    @OnCefMessage("sayHello")
    public String sayHello(JcefFrame jcefFrame, SayHelloReq req) {
        jcefFrame.sendCefMessage("vcl", "anh yeu em 123");
        return "Hello " + req.getName();
    }

    @OnCefMessage("triggerJavaAction")
    public void trigger() {
        SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, "Anh yeu em");
        });
    }

    @OnCefMessage("showJcefFrame")
    public void showJcefFrame(String title) {
        JcefManager.getJcefFrameByTitle(title).forEach(JcefFrame -> JcefFrame.setVisible(true));
    }

    @OnCefMessage("switchScreen")
    public void switchScreen(String title, String keySwitch) {
        JcefManager.getJcefFrameByTitle(title).forEach(JcefFrame -> {
            JcefFrame.getBrowser().loadURL(Main.getServer().getUrlByKey(keySwitch));
            JcefFrame.setTitle("Anh thien dep trai");
        });
    }

    @OnCefMessage("inbox")
    public boolean inbox(String titleTo, String type, Object data) {
        AtomicBoolean success = new AtomicBoolean(false);
        JcefManager.getJcefFrameByTitle(titleTo).forEach(JcefFrame -> {
            JcefFrame.sendCefMessage(type, data);
            success.set(true);
        });
        return success.get();
    }

    @OnCefMessage("broadcast")
    public void broadcast(JcefFrame jcefFrame, String topic, Object data) {
        jcefFrame.broadcastEvent(topic, data);
    }

    @OnCefMessage("getJcefFrameList")
    public Object getJcefFrameList() {
        return JcefManager.getJcefFrames();
    }

    @OnCefMessage("getThisFrame")
    public JcefFrame getThisFrame(JcefFrame jcefFrame, int a, int b, String c) {
        return jcefFrame;
    }
}