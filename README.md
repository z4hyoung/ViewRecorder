# ViewRecorder
A small tool let you record any view which is able to be captured(`SurfaceView` is excluded).

The class is very easy to use, since it extends `MediaRecord` with only one extra API to set or switch recorded view.

Moreover, here is a class named `SurfaceMediaRecorder` which extends `MediaRecord` directly. Video frame is composed periodically according to frame rate, as each frame composing exposed with interface, you can customize it as you want. Meanwhile, you can assign any looper for composing, e.g. a background looper in case of your main thread is heavy or composing is slow. 
