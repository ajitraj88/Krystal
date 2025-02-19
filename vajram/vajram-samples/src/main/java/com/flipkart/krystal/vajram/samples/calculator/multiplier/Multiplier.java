package com.flipkart.krystal.vajram.samples.calculator.multiplier;

import com.flipkart.krystal.vajram.ComputeVajram;
import com.flipkart.krystal.vajram.Input;
import com.flipkart.krystal.vajram.Output;
import com.flipkart.krystal.vajram.VajramDef;
import com.flipkart.krystal.vajram.samples.calculator.multiplier.MultiplierInputUtil.MultiplierInputs;
import java.util.Optional;

@VajramDef
@SuppressWarnings("initialization.field.uninitialized")
public abstract class Multiplier extends ComputeVajram<Integer> {
  @Input int numberOne;
  @Input Optional<Integer> numberTwo;

  @Output
  public static int multiply(MultiplierInputs allInputs) {
    return allInputs.numberOne() * allInputs.numberTwo().orElse(1);
  }
}
