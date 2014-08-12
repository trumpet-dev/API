package com.rms.pilotapi.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.Inject;
import com.mongodb.DB;
import com.rms.pilotapi.core.Person;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.mongojack.internal.MongoJackModule;

public class PersonDaoMongoImpl implements PersonDao {

    JacksonDBCollection<Person, Long> personCollection;

    @Inject
    public PersonDaoMongoImpl(DB mongoDb) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MongoJackModule.configure(objectMapper);
        personCollection = JacksonDBCollection.wrap(
                mongoDb.getCollection("personCollection"),
                Person.class,
                Long.class);
    }

    private long getNextId() {
        return personCollection.getCount() + 1;
    }

    @Override
    public Person getPerson(long id) {
        return personCollection.findOneById(id);
    }

    @Override
    public Person createPerson(Person person) {
        long id = getNextId();
        person.setId(id);
        WriteResult wr = personCollection.insert(person);
        return personCollection.findOneById((Long) wr.getSavedId());
    }

    @Override
    public Person updatePerson(long id, Person person) {
        WriteResult wr = personCollection.updateById(id, person);
        if (wr.getError() == null || wr.getError().isEmpty()) {
            return person;
        }
        return null;
    }

    @Override
    public boolean deletePerson(long id) {

        WriteResult wr = personCollection.removeById(id);

        return wr.getError().isEmpty();
    }
}
