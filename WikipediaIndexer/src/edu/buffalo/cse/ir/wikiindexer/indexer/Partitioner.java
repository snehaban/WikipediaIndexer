/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;


/**
 * THis class is responsible for assigning a partition to a given term.
 * The static methods imply that all instances of this class should 
 * behave exactly the same. Given a term, irrespective of what instance
 * is called, the same partition number should be assigned to it.
 */
public class Partitioner 
{
	static int totalPartitions = 1; 
	/*static String bucket1 = "abcdefgh";
	static String bucket2 = "ijkilmno";
	static String bucket3 = "pqrstuvwxyz";*/
	
	/**
	 * Method to get the total number of partitions
	 * THis is a pure design choice on how many partitions you need
	 * and also how they are assigned.
	 * @return: Total number of partitions
	 */
	public static int getNumPartitions() 
	{
		return totalPartitions;
	}
	
	/**
	 * Method to fetch the partition number for the given term.
	 * The partition numbers should be assigned from 0 to N-1
	 * where N is the total number of partitions.
	 * @param term: The term to be looked up
	 * @return The assigned partition number for the given term
	 */
	public static int getPartitionNumber (String term) 
	{
		/*String start = Character.toString(term.charAt(0));	
		
		if(bucket1.contains(start))
			return 1;
		else if(bucket2.contains(start))
			return 2;
		if(bucket3.contains(start))
			return 3;
		else*/ 
		return 0;
	}
}
