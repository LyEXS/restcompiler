#include <stdio.h>
#include <string.h>
#include <stdlib.h>

float average_float_array(float arr[], int size) {
    if (size == 0) return 0.0f;
    float sum = 0.0f;
    for (int i = 0; i < size; i++) {
        sum += arr[i];
    }
    return sum / size;
}
int main(){
int allPassed=0;
int result;

// Test Case 1
float arg0_1[]={1.5, 2.5, 3.5, 4.5};
int arg1_1=4;
result=average_float_array(arg0_1,arg1_1);
{
char resultStr[1024];
char expectedStr[1024];
strcpy(expectedStr,"3.000000");
sprintf(resultStr,"%f",result);
if(strcmp(resultStr,expectedStr)==0)
printf("Test1 passed\n");
else{
printf("Test1 failed : expected '%s', got '%s'\n",expectedStr,resultStr);
allPassed++;
}
}
// Test Case 2
float arg0_2[]={-2.2, -1.1, 0.0, 1.1, 2.2};
int arg1_2=5;
result=average_float_array(arg0_2,arg1_2);
{
char resultStr[1024];
char expectedStr[1024];
strcpy(expectedStr,"0.000000");
sprintf(resultStr,"%f",result);
if(strcmp(resultStr,expectedStr)==0)
printf("Test2 passed\n");
else{
printf("Test2 failed : expected '%s', got '%s'\n",expectedStr,resultStr);
allPassed++;
}
}
// Test Case 3
float arg0_3[]={0.1, 0.2, 0.3};
int arg1_3=3;
result=average_float_array(arg0_3,arg1_3);
{
char resultStr[1024];
char expectedStr[1024];
strcpy(expectedStr,"0.200000");
sprintf(resultStr,"%f",result);
if(strcmp(resultStr,expectedStr)==0)
printf("Test3 passed\n");
else{
printf("Test3 failed : expected '%s', got '%s'\n",expectedStr,resultStr);
allPassed++;
}
}
return allPassed;
}
