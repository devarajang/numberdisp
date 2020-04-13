# numberdisp

Uses redis to generate uniqueue random numbers. To generate 100 random numbers it will use 100 bitset. 
Java code will generate a random number and call lua script with the random number. 
If the bit at the position is not set it will be set and the position is returned. 
If the bit is set (collision) then it will search the next unset bit in the set and return.
