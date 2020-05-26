package xuecl.myblog.test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {
    @RequestMapping(path = {"/helloSpringBoot"})
    public String HelloSpring (){
        System.out.println("hello spring boot");
        return "hello spring boot";
    }
    
    public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		String[][] matrix = new String[permutation(list.size())][list.size()];
		matrix(list, 0, matrix);
		StringBuilder sb = new StringBuilder();
    	for(int i = 0; i< matrix.length; i++) {
    		for(int j = 0; j<matrix[0].length; j++) {
    			if(matrix[i][j] == null) sb.append(0);
    			if(matrix[i][j] != null) sb.append(matrix[i][j]);
    		}
    		sb.append("\n");
    	}
    	System.out.println(sb);
	}
    
    
    
    private static int permutation(int total, int box){
    	if(box == 0) return 1;
    	return total * permutation(total -1 , box - 1);
    }
    
    private static int permutation(int total){
    	return permutation(total, total);
    }
    
    private static void matrix (List<String> list, int startIndex, String[][] strMatrix){
    	int currentSize = list.size();
    	for (int i = 0; i< currentSize; i++) {
    		for(int j = 0; j< permutation(currentSize -1); j++) {
    			strMatrix[j + startIndex + i * permutation(currentSize -1)][currentSize -1] = list.get(i);
    		}
    		if(currentSize == 1) continue;
    		List<String> listTmp = listDeepClone(list);
    		listTmp.remove(i);
    		matrix(listTmp, startIndex + i * permutation(currentSize -1), strMatrix);
    	}
    }
    
    private static List<String> listDeepClone(List<String> srcList){
    	List<String> destList = new ArrayList<>();
    	for(String str: srcList) {
    		destList.add(str);
    	}
    	return destList;
    }
}
