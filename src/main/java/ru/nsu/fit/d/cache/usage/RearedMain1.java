package ru.nsu.fit.d.cache.usage;

//import ru.nsu.fit.d.cache.store.Node;

import ru.nsu.fit.d.cache.app.Node;

import java.io.IOException;

public class RearedMain1 {
	
	public static void main(String[] args) {

		Node<String> node = null;
		try {
			node = new Node<>(
					9002,
					9003,
					"localhost",
					9001,
					"224.1.1.1",
					1027,
					false
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		node.run();
	}
}
