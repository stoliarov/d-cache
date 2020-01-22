package ru.nsu.fit.d.cache.usage;

import ru.nsu.fit.d.cache.app.Node;

import java.io.IOException;

public class WriterMain {
	
	public static void main(String[] args) {

		Node<String> node = null;
		try {
			node = new Node<>(
					9000,
					9001,
					"localhost",
					9001,
					"224.1.1.1",
					1026,
					true
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		node.run();
	}
}
