package io.trane.ndbc.spring.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@Transactional
@ContextConfiguration(classes = CustomerConfig.class)
public class CustomerRepositoryTest {

  @Autowired
  CustomerRepository customerRepo;

  @Test
  public void createSimpleCustomer() {
    final Customer customer = new Customer();
    customer.dob = LocalDate.of(1904, 5, 14);
    customer.name = "Albert";

    final Customer saved = customerRepo.save(customer);

    assertThat(saved.id).isNotNull();

    saved.name = "Hans Albert";

    customerRepo.save(saved);

    final Optional<Customer> reloaded = customerRepo.findById(saved.id);

    assertThat(reloaded).isNotEmpty();

    assertThat(reloaded.get().name).isEqualTo("Hans Albert");
  }
}