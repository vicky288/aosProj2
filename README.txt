1.Put the shell script(launcher.sh, calculateCS.sh, killall.sh) and topology file(config.txt) in folder $HOME/aos2_2 along with the all the class files.
2.Change the permission of all shell scripts to execute using
	eg:- chmod +x launcher.sh
3.Start the program 
	sh launcher.sh
4.Log files per node will be created which contains the final out put of each node.
5.r1.txt will show if there is any critical section execution violation or not.
6.run calculateCS.sh to count number of critical sections executed per node. 

NB:- Need to change the PROJDIR in the launcher.sh if the files are to be executed in some other folder.
     Run killall.sh after every run to kill processes.
