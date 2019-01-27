Title: Building Native UI Toolkit Part 1
Author: Roman Liutikov
Date: 27-01-2019

For a long time, I’ve been thinking about building cross-platform native UI toolkit, mostly for learning purpose. As web UI developer I always wanted to learn how to manage windows, draw raw pixels on a screen and process input on a lower level. I don’t really care if I’ll succeed at this, but surely this is going to be fun.

The first part was to choose rendering library. I decided to go with [Skia](https://skia.org/) which powers Chrome and Flutter. Skia is a cross-platform high-perf vector graphics rendering library. But the most important aspect is that it’s battle-tested on millions of devices and platforms.

Skia itself doesn’t manage windows, it’s only drawing into rendering contexts, such as OpenGL or Vulkan. [GLFW](https://www.glfw.org/) and similar libraries are taking care of all the hustle of managing windows, inputs and providing rendering context across platforms.

Hooking Skia into GLFW window is pretty straightforward. Below you can see a window with an interactive element rendered in Skia.

<video autoplay muted loop preload="auto">
  <source src="/assets/skia-window.mp4" type="video/mp4" />
</video>

When compiled executable size is just ~7MB and it takes ~13MB of RAM.

<img src="/assets/skia-ram.jpeg" />

Another very important part that I don’t really want to implement from the ground up is the layout. Fortunately, there’s a super lightweight library [Yoga Layout](https://yogalayout.com/) that implements a subset of [CSS Flexible Box](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flexible_Box_Layout) model and is used in React Native.

The API is simple and can be abstracted with CSS-like DSL if needed.

<video autoplay muted loop preload="auto">
  <source src="/assets/yoga-layout.mp4" type="video/mp4" />
</video>

The next step is to spend time on writing bindings to GLFW, Skia, and Yoga in a higher level language. I want this to be a Lisp. For now, I see at least two options: [Carp](https://github.com/carp-lang/Carp) (not really high-level) a statically typed lisp with borrow checker, or Common Lisp ([SBCL](http://www.sbcl.org/)) garbage collected canonical Lisp. The first one is a work in progress experiment which makes it less attractive and SBCL while being perfectly fine, produces ~50MB executable because of a lack of tree shaker in non-commercial Lisp implementations. But at least SBCL can apply compression that reduces the size to ~14MB.

One more thing to think about is how to organize UI widgets and build event propagation system. I’ll leave this for the next blog post.
