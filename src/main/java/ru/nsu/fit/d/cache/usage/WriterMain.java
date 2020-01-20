package ru.nsu.fit.d.cache.usage;

import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.channel.MessageType;
import ru.nsu.fit.d.cache.console.Data;
import ru.nsu.fit.d.cache.store.Node;
import ru.nsu.fit.d.cache.tools.Serializer;

import java.io.IOException;

public class WriterMain {
	
	public static void main(String[] args) {

		Node<String> node = null;
		try {
			node = new Node<>(
					1025,
					"localhost",
					1025,
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
