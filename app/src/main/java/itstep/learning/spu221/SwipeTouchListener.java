package itstep.learning.spu221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public SwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context,new SwipeGestureListner());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
        //outer interace - для реализации в кеоде
    public void onSwipeLeft()   {}
    public void onSwipeRight()  {}
    public void onSwipeBottom() {}
    public void onSwipeTop()    {}



    private final class SwipeGestureListner extends GestureDetector.SimpleOnGestureListener {
        private static final int minDistance = 50;
        private static final int minVelocity = 100;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true; // начало жеста свайпа
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
           boolean isHandled = false;
           if(e1 != null) {
               float deltaX = e2.getX() - e1.getX();
               float deltaY = e2.getY() - e1.getY();
               // Вичислить какого типа это свайп вертикальный или горизонтальный(или никакой)
               //критерии одна из дельт дважды больше чекм другая
               float absX = Math.abs(deltaX);
               float absY = Math.abs(deltaY);
               if(absX>= 2 * absY)//горизонталь жест
               {
                   if(absX >= minDistance && Math.abs(velocityX) > minVelocity){
                       if(deltaX>0){ // e1.X   --------> e2.X
                           onSwipeRight();
                       }
                       else{//e2.X  <-------- e1.X
                           onSwipeLeft();
                       }
                       isHandled = true;
                   }
               }
               else if(absY >= 2 * absX){ //вретикальный жест
                   if(absY >= minDistance && Math.abs(velocityY) > minVelocity){
                       if(deltaY>0){        // e1.X
                           onSwipeBottom(); //  ^
                       }                    //  |
                       else{                //  |
                           onSwipeTop();    //e2.X
                       }
                       isHandled = true;
                   }
               }

           }

           return isHandled;
        }
    }
}
/*
* Swipe - жест, распрастранен в мобильных устройствах, который состоит из проведения
* по экрану с разделением этих жестов по горизонтали или вертикали(иногда диоганаль)
* Особенность - такие жесты не распространенные в андроиде и их необходимо обрабатьывать
* через базовый Fling - те же жесты, которые фиксируют координаты двух точек
* (начало и конец), а также скорость движения между ними.
* */