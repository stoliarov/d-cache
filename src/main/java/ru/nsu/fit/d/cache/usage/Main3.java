package ru.nsu.fit.d.cache.usage;

import ru.nsu.fit.d.cache.app.Node;

import java.io.IOException;

public class Main3 {
	
	public static void main(String[] args) {

		Node<String> node = null;
		try {
			node = new Node<>(
					9005,
					9006,
					"localhost",
					9001,
					"224.1.1.1",
					1028,
					false
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		node.run();
	}
}
