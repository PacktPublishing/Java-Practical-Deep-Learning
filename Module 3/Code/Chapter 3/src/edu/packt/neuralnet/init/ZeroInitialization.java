/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.packt.neuralnet.init;

/**
 *
 * @author fab
 */
public class ZeroInitialization extends WeightInitialization {
    
    @Override
    public double Generate(){
        return 0.0;
    }
    
}
