package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

/**
    The MMU class contains the student code that performs the work of
    handling a memory reference.  It is responsible for calling the
    interrupt handler if a page fault is required.

    @OSPProject Memory
*/
public class MMU extends IflMMU
{
    /** 
        This method is called once before the simulation starts. 
	Can be used to initialize the frame table and other static variables.

        @OSPProject Memory
    */
    public static void init()
    {
        //Initialize frames (memory)
		for(int i = 0 ; i < MMU.getFrameTableSize() ; i++){
			MMU.setFrame(i, new FrameTableEntry(i));
		}

    }

    /**
       This method handlies memory references. The method must 
       calculate, which memory page contains the memoryAddress,
       determine, whether the page is valid, start page fault 
       by making an interrupt if the page is invalid, finally, 
       if the page is still valid, i.e., not swapped out by another 
       thread while this thread was suspended, set its frame
       as referenced and then set it as dirty if necessary.
       (After pagefault, the thread will be placed on the ready queue, 
       and it is possible that some other thread will take away the frame.)
       
       @param memoryAddress A virtual memory address
       @param referenceType The type of memory reference to perform 
       @param thread that does the memory access
       (e.g., MemoryRead or MemoryWrite).
       @return The referenced page.

       @OSPProject Memory
    */
    static public PageTableEntry do_refer(int memoryAddress,
					  int referenceType, ThreadCB thread)
    {
		//MyOut.print(MMU.class, "+++ In do_refer(...), referenceType = " + printableRequest(referenceType));
	
		//Do the calculation to find the page where the referred memory address belong
        
		boolean LRU=false;
		int offsetBitLength = MMU.getVirtualAddressBits() - MMU.getPageAddressBits();
		//MyOut.print(MMU.class, "+++ In do_refer(...), offsetBitLength = " + offsetBitLength);
		
		int pageSize = (int) Math.pow(2, offsetBitLength);
		//MyOut.print(MMU.class, "+++ In do_refer(...), pageSize = " + pageSize);
		
		int pageNumber = memoryAddress/pageSize;
		//MyOut.print(MMU.class, "+++ In do_refer(...), pageNumber = " + pageNumber);
		
		//Check whether the page is valid, if not, then 
		//check whether any thread has validated the page before, if not (page.getValidatingThread() == null),
		//then issue a pageFault interrupt.
		//if so, then suspend this thread on the event of the page.
		PageTableEntry page = thread.getTask().getPageTable().pages[pageNumber];
		if(!page.isValid()){
			if(page.getValidatingThread() == null){
				InterruptVector.setPage(page);
				InterruptVector.setReferenceType(referenceType);
				InterruptVector.setThread(thread);
				CPU.interrupt(PageFault);
				
			}else{
				thread.suspend(page);
				if(thread.getStatus() != ThreadKill){
					page.getFrame().setReferenced(true);
					
					//Set the reference to current time
				//	if(LRU)
				//	page.reftimer = HClock.get();

					if(referenceType == MemoryWrite){
						page.getFrame().setDirty(true);
					}
					
				}
				
				return page;
			}
			
		}
		
		//At this point, the page should be valid.
		//Check whether the thread has been issued to be killed, if not,
		//then we set the reference bit and dirty bit accordingly.
		//The dirty bit, however, is only set when the referenceType is MomoryWrite
		if(thread.getStatus() != ThreadKill){
			page.getFrame().setReferenced(true);
			
			//Set the reference to current time
		//	if(LRU)
		//	page.reftimer = HClock.get();

			if(referenceType == MemoryWrite){
				page.getFrame().setDirty(true);
			}
			
		}
		if(LRU)
		page.reftimer = HClock.get();
		return page;
    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
     
	@OSPProject Memory
     */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
     
      @OSPProject Memory
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
