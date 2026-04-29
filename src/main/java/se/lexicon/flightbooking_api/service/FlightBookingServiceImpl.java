package se.lexicon.flightbooking_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.dto.AvailableFlightDTO;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
import se.lexicon.flightbooking_api.dto.FlightBookingDTO;
import se.lexicon.flightbooking_api.dto.FlightListDTO;
import se.lexicon.flightbooking_api.entity.FlightBooking;
import se.lexicon.flightbooking_api.entity.FlightStatus;
import se.lexicon.flightbooking_api.exception.FlightBookingException;
import se.lexicon.flightbooking_api.exception.ResourceNotFoundException;
import se.lexicon.flightbooking_api.mapper.FlightBookingMapper;
import se.lexicon.flightbooking_api.repository.FlightBookingRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FlightBookingServiceImpl implements FlightBookingService {

    private final FlightBookingRepository flightBookingRepository;
    private final FlightBookingMapper mapper;


    @Override
    public FlightBookingDTO bookFlight(Long flightId, BookFlightRequestDTO bookingRequest) {
        FlightBooking flight = flightBookingRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        if (flight.getStatus() != FlightStatus.AVAILABLE) {
            throw new FlightBookingException("Flight is not available");
        }

        flight.setPassengerName(bookingRequest.passengerName());
        flight.setPassengerEmail(bookingRequest.passengerEmail());
        flight.setStatus(FlightStatus.BOOKED);

        FlightBooking savedFlight = flightBookingRepository.save(flight);
        return mapper.toDTO(savedFlight);
    }

    @Override
    public void cancelFlight(Long flightId, String passengerEmail) {
        FlightBooking flight = flightBookingRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        if (flight.getStatus() != FlightStatus.BOOKED) {
            throw new FlightBookingException("Flight is not currently booked");
        }

        if (flight.getPassengerEmail() == null || !flight.getPassengerEmail().equals(passengerEmail)) {
            throw new FlightBookingException("Passenger email does not match");
        }

        flight.setPassengerName(null);
        flight.setPassengerEmail(null);
        flight.setStatus(FlightStatus.AVAILABLE);
        flightBookingRepository.save(flight);
    }

    @Override
    public List<AvailableFlightDTO> findAvailableFlights() {
        return flightBookingRepository.findByStatus(FlightStatus.AVAILABLE)
                .stream()
                .map(mapper::toAvailableFlightDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightBookingDTO> findBookingsByEmail(String email) {
        return flightBookingRepository.findByPassengerEmailAndStatus(email, FlightStatus.BOOKED)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightListDTO> findAll() {
        return flightBookingRepository.findAll()
                .stream()
                .map(mapper::toListDTO)
                .collect(Collectors.toList());
    }

}
