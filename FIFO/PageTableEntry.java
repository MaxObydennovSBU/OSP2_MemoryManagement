package osp.Memory;

import osp.Hardware.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Devices.*;
import osp.Utilities.*;
import osp.IFLModules.*;
/**
   The PageTableEntry object contains information about a specific virtual
   page in memory, including the page frame in which it resides.
   
   @OSPProject Memory

*/

public class PageTableEntry extends IflPageTableEntry
{
    /**
       The constructor. Must call

       	   super(ownerPageTable,pageNumber);
	   
       as its first statement.

       @OSPProject Memory
    */

	public long reftimer;
    public PageTableEntry(PageTable ownerPageTable, int pageNumber)
    {
        // your code goes here
		super(ownerPageTable, pageNumber);
		reftimer = HClock.get();
		//MyOut.print(PageTableEntry.class, "+++ In PageTableEntry constructor(...), ownerPageTable = " + ownerPageTable);
		//MyOut.print(PageTableEntry.class, "+++ In PageTableEntry constructor(...), pageNumber = " + pageNumber);
    }

    /**
       This method increases the lock count on the page by one. 

	The method must FIRST increment lockCount, THEN  
	check if the page is valid, and if it is not and no 
	page validation event is present for the page, start page fault 
	by calling PageFaultHandler.handlePageFault().

	@return SUCCESS or FAILURE
	FAILURE happens when the pagefault due to locking fails or the 
	that created the IORB thread gets killed.

	@OSPProject Memory
     */
    public int do_lock(IORB iorb)
    {
        //Is the page valid?
		//If not, then is there any thread calling page fault on this page before (getValidatingThread())
		//If it is null, meaning there is no such thread, then go ahead and handle page fault
		//(Right here, I'm not sure whether we do it right. But we also say that if pagefault handler
		//returned Failure, then this function also returns Failure.)
		//If the validating thread is equal to this thread (iorb.getThread)
		//then just increment lock count on the frame and return SUCCESS
		//Otherwise, just suspend this thread on PageTableEntry event(iorb.getThread())
		ThreadCB iorbThread = iorb.getThread();
		boolean LRU=false;

		//Update the refernec time
		if(LRU)
		reftimer = HClock.get();

		if(!isValid()){
			if(getValidatingThread() == null){
				int pageFaultResult = PageFaultHandler.handlePageFault(iorbThread, MemoryLock, this);
				/*if(pageFaultResult == FAILURE){
					return FAILURE;
				}*/
			}else if(getValidatingThread() == iorbThread){
				getFrame().incrementLockCount();
				return SUCCESS;
			}else{
				iorb.getThread().suspend(this);
			}
		}
		
		//If the thread has been issued to be killed, OR the page is still invalid, 
		//then return FAILURE.
		if(iorbThread.getStatus() == ThreadKill || !isValid()){
			return FAILURE;
		}
		
		//Otherwise, increment the lock on the frame and return SUCCESS
		getFrame().incrementLockCount();

		return SUCCESS;
    }

    /** This method decreases the lock count on the page by one. 

	This method must decrement lockCount, but not below zero.

	@OSPProject Memory
    */
    public void do_unlock()
    {
		//Decrement the lock count (as long as the lock count is greater than 0).
        if(getFrame().getLockCount() > 0){
			getFrame().decrementLockCount();
		}
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
