public class LinkedList<T> {
	
	Node<T> first = null;
	private int length = 0;
	
	void add(T var) {
		if(first == null) {
			first = new Node<T>(var);
			length++;
			return;
		}
		
		
		Node<T> currentNode = first;
		
		while(currentNode.next != null) {
			currentNode = currentNode.next;
		}
		length++;
		currentNode.next = new Node<T>(var);
	}
	
	void remove(T var) {
		Node<T> currentNode = first;
		Node<T> previousNode = null;
		
		while(currentNode != null) {
			if(currentNode.variable == var) {
				if(previousNode == null) {
					first = currentNode.next;
					length--;
					return;
				}else {
					previousNode.next = currentNode.next;
					length--;
					return;
				}
			}
			previousNode = currentNode;
			currentNode = currentNode.next;
		}
	}
	
	int length() {
		return length;
	}
}
