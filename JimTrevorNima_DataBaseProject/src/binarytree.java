
public class binarytree {

	bNode root = null;
	bNode current;
	bNode parent;
	
	public class bNode{
		public bNode left, right;
		public String var;
		
		bNode(String Var){
			var = Var;
		}
	}
	
	void add(String v){
		if(root == null){
			root = new bNode(v);
			return;
		}else{
			current = root;
			while(current != null){
				if(v.compareTo(current.var) > 0){
					parent = current;
					current = current.left;
				} else if(v.compareTo(current.var) < 0){
					parent = current;
					current = current.right;
				}else{
					System.out.println("Same");
					return;
				}
			}
			if(v.compareTo(parent.var) > 0){
				parent.left = new bNode(v);
			}else{
				parent.right = new bNode(v);
			}
		}
	}

	void Print(){
		PrintSorted(root);
	}
	
	void PrintSorted(bNode roo){
		if(roo == null){
			return;
		}
		PrintSorted(roo.left);
		System.out.println(roo.var);
		PrintSorted(roo.right);
	}
	
	
	public static void main(String[] args){
		binarytree test = new binarytree();
		
		test.add("a");
		test.add("b");
		test.add("c");
		test.add("B");
		test.add("9");
		test.add("z");
		test.add("H");
		test.add("P");
		
		test.Print();
		
	}
	
	
	
}
