package ru.nsu.fit.d.cache.usage;

//import ru.nsu.fit.d.cache.store.Node;

import ru.nsu.fit.d.cache.store.Node;

import java.io.IOException;

public class RearedMain1 {
	
	public static void main(String[] args) {

		Node<String> node = null;
		try {
			node = new Node<>(
					1027,
					"localhost",
					1025,
					"224.1.1.1",
					1026,
					false
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		node.run();
	}
}
