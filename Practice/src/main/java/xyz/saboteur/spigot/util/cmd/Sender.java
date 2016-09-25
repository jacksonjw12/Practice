package xyz.saboteur.spigot.util.cmd;

import com.sk89q.intake.parametric.annotation.Classifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Classifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Sender {

}