# 청각장애인을 위한 의사소통 보조 어플리케이션 (들림e)


작품소개영상 : https://youtu.be/-fkj7kayiqc


개발환경,구성: 안드로이드스튜디오,Firebase Realtime Database , Android Speech To Text API, Text To Speech API 


작품목적: 아직까지도 우리사회에서 다수의 청각장애인은 일반인들과 동떨어져 자연스레 그들끼리 사회를 형성하거나 홀로 지내는경우가 많다.
      그들이 소외되어가는 가장 큰 이유는 일반인들과의 의사소통이 힘들기때문이다. 만약 스마트폰으로 이를 가능하게하면 그들에게 도움이 
      될 수 있지 않을까? 
       
       
기대효과: 청각장애인은 우리의 어플리케이션을 이용해 비장애인과 1:1로 의사소통을 할 수 있으며 더 나아가 교실에서 수업을 듣는상황에서 
      선생님에게 질문, 들리지않는 선생님의 수업내용을 텍스트로 받아적을 수 있다. 마지막으로 비장애인과 전화통화 역시 원활히 수행할 수 있다.
           
         
소통기능 : 비장애인의 음성을 인식하여 텍스트로 스마트폰의 화면에 출력, 농인이 입력한 텍스트를 전자음성으로 비장애인에게 전달 



수업기능 : 파이어베이스의 실시간 데이터베이스에 저장 된 비장애인의 음성텍스트를 복수의 농인들의 어플UI에 텍스트로 출력한다.
          또한 청각장애인이 요청이 담긴 텍스트를 스피커로 출력하여 비장애인이 인식하도록 한다.



통화기능 : 파이어베이스의 실시간 데이터베이스와 푸쉬알림기능을 이용해 실제 전화통화기능과 유사하게 구현하였다. 
          비장애인의 음성이 인식되면 데이터베이스를 거쳐 농인의 스마트폰에 텍스트로 출력이 된다. 농인은 이를 인지하고 
          텍스트를 입력하면 역시 데이터베이스를 거쳐 비장애인의 스마트폰의 스피커로 전자음성이 출력된다. 
          
          
카카오연동: 카카오톡계정과 연동하여 계정생성, 로그인기능을 보다 간편하고 쉽게 구현하였고 어플사용자의 친구목록을 불러옴으로써
            통화기능을 수행할 수 있다.
         
          
          
         
