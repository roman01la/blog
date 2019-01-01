Title: Web developer makes native app
Author: Roman Liutikov
Date: January 1, 2019
Comments: https://www.reddit.com/r/programming/comments/abiu4z/web_developer_makes_native_app/

The last couple of weeks I’ve been [trying myself](https://github.com/replete-repl/replete-android) at native mobile apps development for Android. Being mainly front-end developer while working with Clojure I’ve chosen to go with Kotlin since it proposes more functional alternative to Java and takes advantage of its ecosystem.

I’ve never done Android before and I should say that it felt surprisingly good, especially because the only mobile dev that I did was in React Native.

The best thing is certainly an IDE (I’m using IntelliJ IDEA). When you are unfamiliar with a language and a platform you are developing for, it’s super easy to get lost and spend a lot of time going through examples and documentation. As a younger developer, I always wondered why those lazy Java oldies use massive IDEs and can’t just remember everything. I’m still far from being old, but my brain has more important things to remember, than a bunch of public interfaces and signatures.

Playing with Kotlin I’m kinda missing simplicity of functions and data as in Clojure or even in JavaScript, but it’s not a big deal to be honest. After all, you pay this price for making a native app that runs much better than React Native pile of rocks (RN is great for its purpose, I just don’t think that worth is always better). The overall dev experience is pretty good for my taste. You have all the necessary tools at hand: stepping debugger, layout debugger, and even fast view reload without restarting the whole app. Platform code is very well documented which is super helpful in combination with “go to definition”.

As web UI dev I’m super jealous about native UI development in that you have more control over runtime, you can easily spin up a thread, have access to hardware, etc. At the same time, it’s amazing to have a technology, such as web, that lets you go from zero to something in no time and distribute your work immediately with a link, like this blog post that I’m writing now. I also know how native UI folks are jealous of web’s fast feedback loop and ease of styling, despite that web platform is far from being ideal for rich UIs. Web still misses a lot of fundamental primitives and its legacy is the worst enemy.

Ideally, I’d like to have a switch in browsers to turn off all machinery that is not needed for my app and ship parts such as layout and styling primitives written in whatever language on top of any OS graphics technology, such as OpenGL. I know this sounds idealistic, but it comes from a desire to be in control of platform and environment, and avoid being dependent on bloated runtime. Of course, this requires more effort on developer’s side, quality software never been easy.
