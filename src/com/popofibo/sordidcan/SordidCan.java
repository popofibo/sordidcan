package com.popofibo.sordidcan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SordidCan {
	public static void main(String[] args) throws Exception {
		if (args == null)
			throw new Exception("Please enter the correct value/s");

		int days = Integer.parseInt(args[0]);
		Date date = new Date();
		if (args.length > 1)
			date = new SimpleDateFormat("dd/MM/yyyy").parse(args[1]);

		Random random = new Random();
		random.nextInt(10);

		for (int i = 0; i < days; i++) {
			int commits = random.nextInt(5) + 1;
			Calendar currentDate = Calendar.getInstance();
			currentDate.setTime(date);
			currentDate.add(Calendar.DAY_OF_YEAR, -i);
			String dateStr = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy")
					.format(currentDate.getTime()) + " +0530";

			for (int j = 0; j < commits; j++) {
				Process echo = Runtime.getRuntime().exec(
						"cmd.exe /c echo " + dateStr + random.nextInt(10000)
								+ " > realwork.txt;");
				echo.waitFor();
				
				Process add = Runtime.getRuntime()
						.exec("cmd.exe /c git add -A");
				add.waitFor();
				Map<String, String> envMap = new HashMap<String, String>();
				envMap.put("GIT_AUTHOR_DATE", dateStr);
				envMap.put("GIT_COMMITTER_DATE", dateStr);

				setEnv(envMap);
				System.out.println(System.getenv("GIT_COMMITTER_DATE"));

				/*
				 * Process commit = Runtime.getRuntime().exec(
				 * "GIT_AUTHOR_DATE='" + dateStr + "' GIT_COMMITTER_DATE='" +
				 * dateStr + "' cmd.exe /c git commit -m 'update'");
				 */
				Process commit = new ProcessBuilder("cmd", "/C",
						"git commit -m 'update'").start();
				commit.waitFor();
				System.out.println("commit worked or not "
						+ commit.exitValue()
						+ new BufferedReader(new InputStreamReader(commit
								.getErrorStream())).readLine());
				Thread.sleep(500);
				/*// Thread.sleep(500);
				Process push = new ProcessBuilder("cmd", "/C",
						"git push -f bakers master").start();

				StreamGobbler outputGobbler = new StreamGobbler(
						push.getInputStream(), "OUTPUT");
				
				System.out.println("Push worked or not : "
						+ outputGobbler.getName());*/
			}

		}

	}

	protected static void setEnv(Map<String, String> newenv) {
		try {
			Class<?> processEnvironmentClass = Class
					.forName("java.lang.ProcessEnvironment");
			Field theEnvironmentField = processEnvironmentClass
					.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			Map<String, String> env = (Map<String, String>) theEnvironmentField
					.get(null);
			env.putAll(newenv);
			Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
					.getDeclaredField("theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField
					.get(null);
			cienv.putAll(newenv);
		} catch (NoSuchFieldException e) {
			try {
				Class[] classes = Collections.class.getDeclaredClasses();
				Map<String, String> env = System.getenv();
				for (Class cl : classes) {
					if ("java.util.Collections$UnmodifiableMap".equals(cl
							.getName())) {
						Field field = cl.getDeclaredField("m");
						field.setAccessible(true);
						Object obj = field.get(env);
						Map<String, String> map = (Map<String, String>) obj;
						map.clear();
						map.putAll(newenv);
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}

class StreamGobbler extends Thread {
	InputStream is;
	String type;

	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(type + "> " + line);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
