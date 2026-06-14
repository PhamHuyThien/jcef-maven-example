package home.thienph.controllers;

import home.thienph.anotations.CefController;
import home.thienph.anotations.OnCefMessage;
import home.thienph.data.dto.SayHelloReq;
import home.thienph.jcefs.JcefWindow;

import javax.swing.*;

@CefController
public class ExampleController {

    @OnCefMessage("sayHello")
    public String sayHello(JcefWindow window, SayHelloReq req) {
        window.sendCefMessage("vcl", "anh yeu em 123");
        return "Hello " + req.getName();
    }

    @OnCefMessage("triggerJavaAction")
    public void trigger(JcefWindow window) {
        SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, "Anh yeu em");
        });
    }

    @OnCefMessage("broadcast")
    public void broadcast(JcefWindow window, String topic, Object data) {
        window.broadcastEvent(topic, data);
    }
}