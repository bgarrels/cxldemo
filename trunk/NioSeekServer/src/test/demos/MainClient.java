package test.demos;

import test.demos.PushService.MessageArrivalCallBack;

public class MainClient {
	public static void main(String[] args) {
		PushService ps = new PushService("localhost", 55555, "10000000000");
		ps.setMessageArrivalCallBack(new MessageArrivalCallBack() {
			public void arrivaled(String msg) {
				//msgΪ���յ�����Ϣ
				System.out.println("���յ�����Ϣ�ǣ�" + msg);
			}
		});
		ps.start();
	}
}
