package pattern;

public class UnitTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 String endpointName = null;
		String entityClass = "Product";
		String dtoClass = "ProductDto";
		String scriptservice = "CreateMyProduct";
		String injectedFieldName = null ;
		
		EndpointGenerator endpointGenerator=new EndpointGenerator(entityClass, dtoClass, scriptservice, injectedFieldName);
		endpointGenerator.generate();
	}

}
