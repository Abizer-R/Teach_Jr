# Teach_Jr
WIFI-based attendance app

------------------------------------------
# Features
1. Detects and marks attendance of students present in classroom physically. This is done with the help of ***__WIFI-Direct Service Discovery__***.

2. Provides Excel sheet of attendance to teachers using ***__APACHE POI Library__***.

3. Attendance tracking is made easy for both teachers and students.

------------------------------------------
# Teacher's ScreenShots ( Video : [LINK](https://drive.google.com/file/d/173AZCf006p21hBNyQdpRa-OiC23xdtWE/view?usp=share_link) )
![COMPILED_SS_1](https://user-images.githubusercontent.com/86946600/223029666-588d4f80-6455-4148-a93a-0ba4bf8d355b.jpg)


------------------------------------------

### If Internet is not available
1. Displays the stored data from the database.
## Demo
![Screenshot_Collage jpg](https://user-images.githubusercontent.com/86946600/159305554-b4b693d5-bc39-4d8a-affe-10ed4336465b.png)

[Click here](https://drive.google.com/file/d/1tyax458TjD_Au6etMGLA_AjAHVCNArrR/view?usp=sharing) to watch a video demo.
## Tutorial Links

 - [Retrofit Tutorial](https://www.youtube.com/playlist?list=PLrnPJCHvNZuCbuD3xpfKzQWOj3AXybSaM)
 - [MVVM Architecture + ROOM](https://www.youtube.com/playlist?list=PLrnPJCHvNZuDihTpkRs6SpZhqgBqPU118)


## Small edge cases I considered, that I think helps in improving user experience

- Shows GPS is on/off even if internet is off
- Doesn't ask GPS permission if internet is off 
   cuz it doesn't matter.
- Doesn't ask for GPS again if the user hits "ignore" 
   (Unless user wishes to enable it).
