package com.chien.agricultural.service;

import com.chien.agricultural.dto.request.AddressRequest;
import com.chien.agricultural.dto.response.AddressResponse;
import com.chien.agricultural.exception.AppException;
import com.chien.agricultural.model.Address;
import com.chien.agricultural.model.User;
import com.chien.agricultural.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(); // Của thư viện JTS

    // 1. Lấy danh sách địa chỉ
    public List<AddressResponse> getMyAddresses() {
        String userId = getCurrentUserId();
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 2. Thêm mới địa chỉ
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {

        String userId = getCurrentUserId();

        // Luôn bỏ default cũ
        addressRepository.resetDefaultAddress(userId);

        Address address = new Address();
        mapRequestToEntity(request, address);

        address.setIsDefault(true);
        address.setUser(User.builder().id(userId).build());

        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }


    // 3. Cập nhật địa chỉ
    @Transactional
    public AddressResponse updateAddress(String addressId, AddressRequest request) {

        String userId = getCurrentUserId();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException("Address not found", HttpStatus.NOT_FOUND));

        // Security
        if (!address.getUser().getId().equals(userId)) {
            throw new AppException("You do not have permission", HttpStatus.FORBIDDEN);
        }

        // Map các field thông thường (KHÔNG map isDefault)
        mapRequestToEntity(request, address);

        // Logic default
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.resetDefaultAddress(userId);
            address.setIsDefault(true);
        }

        if (Boolean.FALSE.equals(request.getIsDefault()) && address.getIsDefault()) {
            throw new AppException("At least one address must be default", HttpStatus.BAD_REQUEST);
        }

        return mapToResponse(address);
    }


    // 4. Xóa địa chỉ
    @Transactional
    public void deleteAddress(String addressId) {
        String userId = getCurrentUserId();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException("Address not found", HttpStatus.NOT_FOUND));

        if (!address.getUser().getId().equals(userId)) {
            throw new AppException("Forbidden", HttpStatus.FORBIDDEN);
        }

        // Nếu xóa địa chỉ mặc định -> Phải chỉ định địa chỉ khác làm mặc định (nếu còn)
        // (Logic đơn giản: Cấm xóa địa chỉ mặc định, bắt user chọn cái khác làm default trước)
        if (address.getIsDefault()) {
            throw new AppException("Cannot delete default address. Please set another address as default first.", HttpStatus.BAD_REQUEST);
        }

        addressRepository.delete(address);
    }


    public AddressResponse getAddressById(String id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AppException("Address not found", HttpStatus.NOT_FOUND));
        return mapToResponse(address);
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void mapRequestToEntity(AddressRequest req, Address entity) {
        entity.setRecipientName(req.getRecipientName());
        entity.setPhone(req.getPhone());
        entity.setDetailAddress(req.getDetailAddress());
        entity.setProvinceId(req.getProvinceId());
        entity.setProvinceName(req.getProvinceName());
        entity.setDistrictId(req.getDistrictId());
        entity.setDistrictName(req.getDistrictName());
        entity.setWardCode(req.getWardCode());
        entity.setWardName(req.getWardName());

        // Xử lý POSTGIS Point
        if (req.getLatitude() != null && req.getLongitude() != null) {
            // Point(Longitude, Latitude) -> Nhớ đúng thứ tự
            Point point = geometryFactory.createPoint(new Coordinate(req.getLongitude(), req.getLatitude()));
            point.setSRID(4326); // WGS84
            entity.setLocation(point);
        }
    }

    private AddressResponse mapToResponse(Address entity) {
        return AddressResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId()) // Nếu ID là UUID thì entity.getId().toString()
                .recipientName(entity.getRecipientName())
                .phone(entity.getPhone())
                .detailAddress(entity.getDetailAddress())
                .provinceId(entity.getProvinceId())
                .provinceName(entity.getProvinceName())
                .districtId(entity.getDistrictId())
                .districtName(entity.getDistrictName())
                .wardCode(entity.getWardCode())
                .wardName(entity.getWardName())
                .isDefault(entity.getIsDefault())
                // Convert Point -> Lat/Lon
                .latitude(entity.getLocation() != null ? entity.getLocation().getY() : null)
                .longitude(entity.getLocation() != null ? entity.getLocation().getX() : null)
                .build();
    }
}
